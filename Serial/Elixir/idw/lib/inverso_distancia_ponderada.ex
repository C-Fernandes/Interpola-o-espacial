defmodule Idw.InversoDistanciaPonderada do
  alias Idw.Ponto
  @raio_terra_km 6371.0

  @spec distancia_geografica(Ponto.t(), Ponto.t()) :: float()
  def distancia_geografica(%Ponto{} = a, %Ponto{} = b) do
    dphi = b.lat_rad - a.lat_rad
    dlambda = b.lon_rad - a.lon_rad

    a_val =
      :math.sin(dphi / 2) ** 2 +
        :math.cos(a.lat_rad) * :math.cos(b.lat_rad) * :math.sin(dlambda / 2) ** 2

    2 * @raio_terra_km * :math.atan2(:math.sqrt(a_val), :math.sqrt(1 - a_val))
  end

  @spec vizinhos_mais_proximos(Ponto.t(), [Ponto.t()], non_neg_integer()) :: [Ponto.t()]
  def vizinhos_mais_proximos(%Ponto{} = alvo, lista, k) do
    lista
    |> Enum.map(fn p -> {distancia_geografica(alvo, p), p} end)
    |> Enum.sort_by(fn {d, _p} -> d end)
    |> Enum.take(k)
    |> Enum.map(fn {_d, p} -> p end)
  end

  @spec interpolar_com_vizinhos(Ponto.t(), [Ponto.t()], number()) :: float() | nil
  def interpolar_com_vizinhos(%Ponto{} = alvo, vizinhos, p) do
    eps = 1.0e-9

    case Enum.filter(vizinhos, fn v -> distancia_geografica(alvo, v) < eps end) do
      iguais when iguais != [] ->
        iguais
        |> Enum.map(& &1.temperatura)
        |> then(fn temps -> Enum.sum(temps) / length(temps) end)

      _ ->
        {num, den} =
          Enum.reduce(vizinhos, {0.0, 0.0}, fn v, {acc_n, acc_d} ->
            d = distancia_geografica(alvo, v)

            if d < eps do
              {acc_n + v.temperatura, acc_d + 1.0}
            else
              peso = 1.0 / :math.pow(d, p)
              {acc_n + peso * v.temperatura, acc_d + peso}
            end
          end)

        if den == 0.0, do: nil, else: num / den
    end
  end
end
