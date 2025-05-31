defmodule Idw.LeitorCSV do
  alias Idw.{Ponto, InversoDistanciaPonderada}

  @spec ler_e_interpolar(String.t()) :: :ok
  def ler_e_interpolar(caminho_csv) do
    p = 2
    k = 5

    # abre arquivos
    {:ok, file} = File.open(caminho_csv, [:read])
    {:ok, output} = File.open("saida_interpolacao.txt", [:write])

    # pula o cabeçalho
    IO.read(file, :line)

    # inicia recursão
    ler_linhas(file, output, p, k, [], [], nil)

    File.close(file)
    File.close(output)
  end

  # leitura recursiva linha a linha
  defp ler_linhas(file, output, p, k, precisa_interpolar, pontos_validos, data_momento) do
    case IO.read(file, :line) do
      :eof ->
        # final do arquivo: escreve o que falta interpolar
        escreve_interpolacao(output, p, precisa_interpolar, pontos_validos)

      linha ->
        # faz o parse
        [_, data, hora, temp_s, lat_s, lon_s] =
          linha |> String.trim() |> String.split(",")

        latitude = String.to_float(lat_s)
        longitude = String.to_float(lon_s)
        temperatura = String.to_float(temp_s)

        # cria struct Ponto com radianos pré-calculados
        ponto = Ponto.novo(latitude, longitude, temperatura, data, hora)

        # se mudou a data, processa o que faltou da data anterior
        {precisa_interpolar, pontos_validos, data_momento} =
          if data_momento == nil or data_momento != data do
            escreve_interpolacao(output, p, precisa_interpolar, pontos_validos)
            {[], [], data}
          else
            {precisa_interpolar, pontos_validos, data_momento}
          end

        # decide se guarda como válido ou marca para interpolar
        {precisa_interpolar, pontos_validos} =
          cond do
            ponto.temperatura == -9999.0 ->
              {[ponto | precisa_interpolar], pontos_validos}

            precisa_interpolar != [] and data == data_momento ->
              # atualiza vizinhança de quem precisa interpolar
              viz_atualizados =
                Enum.map(precisa_interpolar, fn pi ->
                  if pi.hora == hora do
                    InversoDistanciaPonderada.vizinhos_mais_proximos(pi, pontos_validos, k)
                    |> then(&%{pi | pontos_proximos: &1})
                  else
                    pi
                  end
                end)

              {viz_atualizados, pontos_validos}

            true ->
              {precisa_interpolar, [ponto | pontos_validos]}
          end

        # recusa para próxima linha
        ler_linhas(file, output, p, k, precisa_interpolar, pontos_validos, data_momento)
    end
  end

  # escreve interpolação de todos os pontos em precisa_interpolar
  defp escreve_interpolacao(output, p, [], _pontos_validos), do: :ok

  defp escreve_interpolacao(output, p, precisa_interpolar, _pontos_validos) do
    Enum.each(precisa_interpolar, fn ponto ->
      interp =
        InversoDistanciaPonderada.interpolar_com_vizinhos(ponto, ponto.pontos_proximos, p)

      IO.write(
        output,
        ">>> Interpolando #{ponto.data} #{ponto.hora} " <>
          "(#{Float.round(ponto.latitude, 4)},#{Float.round(ponto.longitude, 4)})\n" <>
          "Original: #{ponto.temperatura} | Interpolado: " <>
          if(interp, do: "#{Float.round(interp, 2)}°C", else: "sem vizinhos") <>
          "\n" <>
          "Pontos usados:\n" <>
          Enum.map_join(ponto.pontos_proximos, "", fn v ->
            "- (#{v.latitude},#{v.longitude}) t=#{v.temperatura} #{v.data} #{v.hora}\n"
          end) <> "\n"
      )
    end)
  end
end
