defmodule Idw do
  alias Idw.LeitorCSV

  def main do
    # 1. Captura as estatísticas de GC iniciais
    {initial_gcs, initial_gc_time_ns, _} = :erlang.statistics(:garbage_collection)

    # 2. Mede o tempo total de execução
    {time_in_microseconds, _result} =
      :timer.tc(fn ->
        caminho_csv = Path.join(:code.priv_dir(:idw), "dataset.csv")

        if File.exists?(caminho_csv) do
          LeitorCSV.ler_e_interpolar(caminho_csv)
          IO.puts("Acabou")
        else
          IO.puts("Arquivo CSV não encontrado em priv/dataset.csv")
        end
      end)

    # 3. Captura as estatísticas de GC finais
    {final_gcs, final_gc_time_ns, _} = :erlang.statistics(:garbage_collection)

    # 4. Calcula os resultados com as unidades corretas
    total_time_ms = time_in_microseconds / 1000
    gc_time_diff_ns = final_gc_time_ns - initial_gc_time_ns
    # Converte de nanossegundos para milissegundos
    gc_time_diff_ms = gc_time_diff_ns / 1_000_000
    gcs_diff = final_gcs - initial_gcs

    # 5. Exibe o relatório final
    IO.puts("\n--- Estatísticas de Execução ---")
    IO.puts("Tempo total de execução: #{:erlang.float_to_binary(total_time_ms, decimals: 3)} ms")
    IO.puts("----------------------------------")
    IO.puts("Tempo gasto em GC: #{:erlang.float_to_binary(gc_time_diff_ms, decimals: 3)} ms")
    IO.puts("Número de coletas de lixo: #{gcs_diff}")
  end
end
