defmodule Idw.LeitorCSV do
  alias Idw.{Ponto, InversoDistanciaPonderada}

  @spec ler_e_interpolar(String.t()) :: :ok
  def ler_e_interpolar(caminho_csv) do
    p = 2
    k = 5

    {:ok, file} = File.open(caminho_csv, [:read])
    # Inicia o processo escritor (recebe iolists e escreve na saída)
    output_pid = spawn_link(fn -> escritor("saida_interpolacao.txt") end)

    # Pula o cabeçalho
    IO.read(file, :line)

    # Inicia a recursão de leitura
    ler_linhas(file, output_pid, p, k, [], [], nil)

    File.close(file)
    # Informa ao escritor que terminou
    send(output_pid, :fim)
  end

  # Leitura recursiva, acumulando blocos por data
  defp ler_linhas(file, output_pid, p, k, precisa_interpolar, pontos_validos, data_momento) do
    case IO.read(file, :line) do
      :eof ->
        # Ao chegar no fim, dispara interpolação para o último bloco
        dispatch_interpolacoes(output_pid, p, k, precisa_interpolar, pontos_validos)

      linha ->
        trimmed = String.trim(linha)

        case String.split(trimmed, ",", parts: 6) do
          [_, data, hora, temp_s, lat_s, lon_s] ->
            latitude = String.to_float(lat_s)
            longitude = String.to_float(lon_s)
            temperatura = String.to_float(temp_s)

            ponto = Ponto.novo(latitude, longitude, temperatura, data, hora)

            {precisa_interpolar, pontos_validos, data_momento} =
              # Se mudou a data, despacha o bloco anterior
              if data_momento == nil or data != data_momento do
                dispatch_interpolacoes(output_pid, p, k, precisa_interpolar, pontos_validos)
                {[], [], data}
              else
                {precisa_interpolar, pontos_validos, data_momento}
              end

            {precisa_interpolar, pontos_validos} =
              if ponto.temperatura == -9999.0 do
                # precisa interpolar depois
                {[ponto | precisa_interpolar], pontos_validos}
              else
                # já é ponto válido
                {precisa_interpolar, [ponto | pontos_validos]}
              end

            ler_linhas(file, output_pid, p, k, precisa_interpolar, pontos_validos, data_momento)

          _ ->
            # linha malformada, ignora
            ler_linhas(file, output_pid, p, k, precisa_interpolar, pontos_validos, data_momento)
        end
    end
  end

  # Usa Task.async_stream em batches de 50 para interpolar todos pontos de um bloco,
  # mantendo a ordem original, e envia cada iolist ao escritor em lotes.
  defp dispatch_interpolacoes(output_pid, p, k, precisa_interpolar, pontos_validos) do
    # Reverte as listas para manter a ordem original de inserção
    pontos_para_interpolar = Enum.reverse(precisa_interpolar)
    pontos_validos = Enum.reverse(pontos_validos)

    # Se não há pontos para interpolar, nada a fazer
    if pontos_para_interpolar == [] do
      :ok
    else
      # 1) Agrupa pontos válidos por hora para lookup O(1)
      validos_por_hora = Enum.group_by(pontos_validos, & &1.hora)

      # 2) Divide pontos_para_interpolar em chunks de 50
      batch_size = 50
      pontos_chunks = Enum.chunk_every(pontos_para_interpolar, batch_size)

      # 3) Define quantos processos paralelos serão usados, respeitando a quantidade de chunks
      total_chunks = length(pontos_chunks)
      max_concurrency = min(System.schedulers_online(), total_chunks)

      # 4) Processa cada chunk em paralelo, mas mantém cada chunk ordenado internamente
      pontos_chunks
      |> Task.async_stream(
        fn chunk ->
          # Cada chunk é uma lista de pontos; itera sequencialmente
          Enum.map(chunk, fn ponto ->
            vizinhos_da_hora =
              Map.get(validos_por_hora, ponto.hora, [])

            vizinhos =
              InversoDistanciaPonderada.vizinhos_mais_proximos(ponto, vizinhos_da_hora, k)

            interp = InversoDistanciaPonderada.interpolar_com_vizinhos(ponto, vizinhos, p)
            monta_iolist(ponto, vizinhos, interp)
          end)
        end,
        max_concurrency: max_concurrency,
        timeout: :infinity,
        ordered: true     # garante que os chunks retornem na ordem original
      )
      |> Enum.each(fn
        {:ok, iolists_chunk} ->
          # Concatena todos os iolists do chunk em uma só binária, para reduzir chamadas I/O
          big_iolist = :erlang.iolist_to_binary(iolists_chunk)
          send(output_pid, {:escrever, big_iolist})

        {:exit, _reason} ->
          :noop
      end)
    end
  end

  # Monta exatamente o mesmo iolist original para cada ponto
  defp monta_iolist(ponto, vizinhos, interp) do
    [
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
  end

  # Processo que escreve iolists em um arquivo, um por vez, na ordem em que chegam
  defp escritor(arquivo_saida) do
    {:ok, file} = File.open(arquivo_saida, [:write])
    loop_escrita(file)
  end

  defp loop_escrita(file) do
    receive do
      {:escrever, iolist} ->
        IO.write(file, iolist)
        loop_escrita(file)

      :fim ->
        File.close(file)
    end
  end
end
