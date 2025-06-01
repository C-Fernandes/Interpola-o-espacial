defmodule Idw do
  alias Idw.LeitorCSV

  def main do
    caminho_csv = Path.join(:code.priv_dir(:idw), "dataset.csv")

    if File.exists?(caminho_csv) do
      LeitorCSV.ler_e_interpolar(caminho_csv)
      IO.puts("Acabou")
    else
      IO.puts("Arquivo CSV não encontrado em priv/dataset.csv")
    end
  end
end
