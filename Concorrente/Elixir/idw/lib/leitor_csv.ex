defmodule Idw.LeitorCSV do
  alias Idw.Ponto
  alias Idw.InversoDistanciaPonderada

  @p 2
  @k 5

  def ler_e_interpolar(caminho_csv) do
    caminho_saida = Path.basename("saida") <> "_interpolacao.txt"

    {:ok, file_writer_pid} = Agent.start(fn -> File.open!(caminho_saida, [:write, :append]) end)

    IO.puts("Iniciando processamento de #{caminho_csv}...")
    IO.puts("Resultados serão escritos em #{caminho_saida}")

    caminho_csv
    |> File.stream!()
    |> Stream.drop(1)
    |> Stream.map(&parse_linha/1)
    |> Stream.chunk_by(& &1.data)
    |> Task.async_stream(
      fn lote_por_data ->
        data_atual = lote_por_data |> hd() |> Map.get(:data)
        processar_lote(lote_por_data, file_writer_pid)
      end,
      max_concurrency: System.schedulers_online(),
      ordered: false
    )
    |> Stream.run()

    Agent.get_and_update(file_writer_pid, fn file_device ->
      File.close(file_device)
      {:ok, nil}
    end)

    IO.puts("Processamento concluído.")
  end

  def processar_lote(lote_pontos, file_writer_pid) do
    {precisa_interpolar, pontos_validos} =
      Enum.split_with(lote_pontos, &(&1.temperatura == -9999.0))

    mapa_por_hora = Enum.group_by(pontos_validos, & &1.hora)

    precisa_interpolar
    |> Task.async_stream(
      fn alvo ->
        candidatos = Map.get(mapa_por_hora, alvo.hora, [])

        com_vizinhos =
          InversoDistanciaPonderada.atualizar_vizinhos_mais_proximos(alvo, candidatos, @k)

        valor_interp = InversoDistanciaPonderada.interpolar_com_vizinhos(com_vizinhos, @p)

        lat_str = :erlang.float_to_binary(com_vizinhos.latitude, decimals: 4)
        lon_str = :erlang.float_to_binary(com_vizinhos.longitude, decimals: 4)
        temp_str = :erlang.float_to_binary(valor_interp, decimals: 2)

        "Ponto: #{com_vizinhos.data} #{com_vizinhos.hora} (#{lat_str}, #{lon_str})\n" <>
          " | Temperatura interpolada: #{temp_str}°C\n"
      end,
      max_concurrency: System.schedulers_online() * 2,
      ordered: false
    )
    |> Stream.each(fn {:ok, resultado} ->
      Agent.update(file_writer_pid, fn file_device ->
        IO.write(file_device, resultado)
        file_device
      end)
    end)
    |> Stream.run()
  end

  defp parse_linha(linha) do
    campos =
      linha
      |> String.trim()
      |> String.split(",")

    [_, data, hora, temp_str, lat_str, lon_str | _] = campos

    Ponto.novo(
      String.to_float(lat_str),
      String.to_float(lon_str),
      String.to_float(temp_str),
      data,
      hora
    )
  end
end
