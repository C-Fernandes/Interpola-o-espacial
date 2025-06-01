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
        # final do arquivo: processa o último bloco de data
        escreve_interpolacao(output, p, k, precisa_interpolar, pontos_validos)

      linha ->
        # usa String.trim e String.split/3 com parts: 6 para quebrar em exatas 6 colunas
        trimmed = String.trim(linha)

        case String.split(trimmed, ",", parts: 6) do
          [_, data, hora, temp_s, lat_s, lon_s] ->
            latitude = String.to_float(lat_s)
            longitude = String.to_float(lon_s)
            temperatura = String.to_float(temp_s)

            # cria struct Ponto com radianos pré-calculados
            ponto = Ponto.novo(latitude, longitude, temperatura, data, hora)

            # se mudou a data, processa todo o bloco anterior
            {precisa_interpolar, pontos_validos, data_momento} =
              if data_momento == nil or data_momento != data do
                escreve_interpolacao(output, p, k, precisa_interpolar, pontos_validos)
                {[], [], data}
              else
                {precisa_interpolar, pontos_validos, data_momento}
              end

            # decide se guarda como válido ou marca para interpolar
            {precisa_interpolar, pontos_validos} =
              if ponto.temperatura == -9999.0 do
                # temperatura faltante: guarda para interpolar depois
                {[ponto | precisa_interpolar], pontos_validos}
              else
                # temperatura válida: acrescenta à lista de pontos válidos
                {precisa_interpolar, [ponto | pontos_validos]}
              end

            # segue para a próxima linha
            ler_linhas(file, output, p, k, precisa_interpolar, pontos_validos, data_momento)

          _ ->
            # linha malformada, ignora e segue
            ler_linhas(file, output, p, k, precisa_interpolar, pontos_validos, data_momento)
        end
    end
  end

  # escreve interpolação de todos os pontos em precisa_interpolar,
  # usando iolists para montar cada saída e Enum.reverse uma única vez
  defp escreve_interpolacao(_output, _p, _k, [], _pontos_validos), do: :ok

  defp escreve_interpolacao(output, p, k, precisa_interpolar, pontos_validos) do
    # inverte uma única vez para preservar a ordem original de leitura
    pontos_validos = Enum.reverse(pontos_validos)
    pontos_para_interpolar = Enum.reverse(precisa_interpolar)

    Enum.each(pontos_para_interpolar, fn ponto ->
      # filtra pontos válidos que têm a mesma hora do ponto a interpolar
      validos_mesma_hora =
        Enum.filter(pontos_validos, fn pv -> pv.hora == ponto.hora end)

      # calcula k vizinhos mais próximos entre esses
      vizinhos = InversoDistanciaPonderada.vizinhos_mais_proximos(ponto, validos_mesma_hora, k)
      # faz a interpolação, usando expoente p
      interp = InversoDistanciaPonderada.interpolar_com_vizinhos(ponto, vizinhos, p)

      # monta iolist em vez de concatenar várias strings
      iolist = [
        ">>> Interpolando ",
        ponto.data,
        " ",
        ponto.hora,
        " (",
        :erlang.float_to_binary(ponto.latitude, decimals: 4),
        ",",
        :erlang.float_to_binary(ponto.longitude, decimals: 4),
        ")\n",
        "Original: ",
        :erlang.float_to_binary(ponto.temperatura, decimals: 1),
        " | Interpolado: ",
        if interp do
          :erlang.float_to_binary(interp, decimals: 2) <> "°C"
        else
          "sem vizinhos"
        end,
        "\nPontos usados:\n",
        Enum.map(vizinhos, fn v ->
          [
            "- (",
            :erlang.float_to_binary(v.latitude, decimals: 4),
            ",",
            :erlang.float_to_binary(v.longitude, decimals: 4),
            ") t=",
            :erlang.float_to_binary(v.temperatura, decimals: 1),
            " ",
            v.data,
            " ",
            v.hora,
            "\n"
          ]
        end),
        "\n"
      ]

      IO.write(output, iolist)
    end)
  end
end
