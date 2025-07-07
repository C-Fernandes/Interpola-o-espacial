defmodule Idw.LeitorCSV do
  alias Idw.{Ponto, InversoDistanciaPonderada}

  @p 2
  @k 5

  @spec ler_e_interpolar(String.t()) :: :ok
  def ler_e_interpolar(caminho_csv) do
    {:ok, file} = File.open(caminho_csv, [:read])
    {:ok, output} = File.open("saida_interpolacao.txt", [:write])

    # pula cabeçalho
    IO.read(file, :line)

    ler_linhas(file, output, [], [], nil)

    File.close(file)
    File.close(output)
  end

  # Função recursiva que lê linha a linha
  defp ler_linhas(file, output, precisa_interpolar, pontos_validos, data_momento) do
    case IO.read(file, :line) do
      :eof ->
        processar_lote(precisa_interpolar, pontos_validos, output)

      linha ->
        case parse_linha(linha) do
          {:ok, ponto} ->
            {precisa_interpolar, pontos_validos, data_momento} =
              if data_momento == nil or data_momento != ponto.data do
                processar_lote(precisa_interpolar, pontos_validos, output)
                {[], [], ponto.data}
              else
                {precisa_interpolar, pontos_validos, data_momento}
              end

            {precisa_interpolar, pontos_validos} =
              if ponto.temperatura == -9999.0 do
                {[ponto | precisa_interpolar], pontos_validos}
              else
                {precisa_interpolar, [ponto | pontos_validos]}
              end

            ler_linhas(file, output, precisa_interpolar, pontos_validos, data_momento)

          :erro ->
            # ignora linha malformada
            ler_linhas(file, output, precisa_interpolar, pontos_validos, data_momento)
        end
    end
  end

  defp parse_linha(linha) do
    linha
    |> String.trim()
    |> String.split(",", parts: 6)
    |> case do
      [_, data, hora, temp_s, lat_s, lon_s] ->
        try do
          latitude = String.to_float(lat_s)
          longitude = String.to_float(lon_s)
          temperatura = String.to_float(temp_s)
          {:ok, Ponto.novo(latitude, longitude, temperatura, data, hora)}
        rescue
          _ -> :erro
        end

      _ ->
        :erro
    end
  end

  defp processar_lote([], _pontos_validos, _output), do: :ok

  defp processar_lote(precisa_interpolar, pontos_validos, output) do
    pontos_validos = Enum.reverse(pontos_validos)

    Enum.reverse(precisa_interpolar)
    |> Enum.each(fn ponto ->
      candidatos = filtrar_por_hora(pontos_validos, ponto.hora)
      vizinhos = InversoDistanciaPonderada.vizinhos_mais_proximos(ponto, candidatos, @k)
      valor = InversoDistanciaPonderada.interpolar_com_vizinhos(ponto, vizinhos, @p)
      escrever_resultado_interpolacao(output, ponto, vizinhos, valor)
    end)
  end

  @spec filtrar_por_hora([Ponto.t()], String.t()) :: [Ponto.t()]
  def filtrar_por_hora(pontos, hora) do
    Enum.filter(pontos, &(&1.hora == hora))
  end

  defp escrever_resultado_interpolacao(output, ponto, vizinhos, valor_interpolado) do
    iolist = [
      ">>> Interpolando ponto em ",
      ponto.data,
      " ",
      ponto.hora,
      " (",
      :erlang.float_to_binary(ponto.latitude, decimals: 4),
      ", ",
      :erlang.float_to_binary(ponto.longitude, decimals: 4),
      ")\n",
      "Temperatura original: ",
      :erlang.float_to_binary(ponto.temperatura, decimals: 2),
      " | Temperatura interpolada: ",
      if valor_interpolado do
        :erlang.float_to_binary(valor_interpolado, decimals: 2) <> "°C"
      else
        "sem vizinhos"
      end,
      "\nPontos usados na interpolação:\n",
      Enum.map(vizinhos, fn v ->
        [
          "- (",
          :erlang.float_to_binary(v.latitude, decimals: 4),
          ", ",
          :erlang.float_to_binary(v.longitude, decimals: 4),
          ") | Temp: ",
          :erlang.float_to_binary(v.temperatura, decimals: 2),
          " | Data: ",
          v.data,
          " Hora: ",
          v.hora,
          "\n"
        ]
      end),
      "\n"
    ]

    IO.write(output, iolist)
  end
end
