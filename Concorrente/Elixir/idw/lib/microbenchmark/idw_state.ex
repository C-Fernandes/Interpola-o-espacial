defmodule IDWState do
  alias Idw.Ponto

  @num_candidatos 10_000

  @spec gerar_estado() :: %{lista_candidatos: [Ponto.t()], ponto_alvo: Ponto.t()}
  def gerar_estado do
    # Semente randômica
    :rand.seed(:exsplus, :os.timestamp())

    create_random_ponto = fn ->
      lat = -10.0 + 0.5 * :rand.uniform(24)
      lon = -40.0 + 0.5 * :rand.uniform(24)
      temp = 35.0 * :rand.uniform()
      data = "2025-06-01"
      hora = String.pad_leading(Integer.to_string(:rand.uniform(24) - 1), 2, "0") <> ":00"
      Ponto.novo(lat, lon, temp, data, hora)
    end

    lista =
      Enum.map(1..@num_candidatos, fn _ -> create_random_ponto.() end)

    idx = :rand.uniform(@num_candidatos)
    base = Enum.at(lista, idx - 1)
    ponto_alvo = Ponto.novo(base.latitude, base.longitude, -9999.0, base.data, base.hora)

    %{lista_candidatos: lista, ponto_alvo: ponto_alvo}
  end
end
