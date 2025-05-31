defmodule Idw.InversoDistanciaPonderada do
  @moduledoc """
  Implementa o algoritmo de Inverso da Distância Ponderada (IDW),
  usando lat_rad/lon_rad pré-calculados em cada %Ponto{}.
  """

  alias Idw.Ponto
  @raio_terra_km 6371.0

  @doc """
  Retorna os k pontos mais próximos de `alvo` dentro de `lista`.
  """
  @spec vizinhos_mais_proximos(Ponto.t(), [Ponto.t()], non_neg_integer()) :: [Ponto.t()]
  def vizinhos_mais_proximos(%Ponto{} = alvo, lista, k) do
    lista
    |> Enum.map(fn p -> {dist_haversine(alvo, p), p} end)
    |> Enum.sort_by(fn {d, _p} -> d end)
    |> Enum.take(k)
    |> Enum.map(fn {_d, p} -> p end)
  end

  @doc """
  Faz a interpolação IDW para o `alvo`, usando os `vizinhos` e potência `p`.
  Retorna temperatura interpolada ou nil se não houver vizinhos.
  """
  @spec interpolar_com_vizinhos(Ponto.t(), [Ponto.t()], number()) :: float() | nil
  def interpolar_com_vizinhos(%Ponto{} = alvo, vizinhos, p) do
    eps = 1.0e-9

    case Enum.filter(vizinhos, fn v -> dist_haversine(alvo, v) < eps end) do
      iguais when iguais != [] ->
        iguais
        |> Enum.map(& &1.temperatura)
        |> then(fn temps -> Enum.sum(temps) / length(temps) end)

      _ ->
        {num, den} =
          Enum.reduce(vizinhos, {0.0, 0.0}, fn v, {acc_n, acc_d} ->
            d = dist_haversine(alvo, v)

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

  # --- Funções privadas abaixo ---

  # Distância pelo Haversine, usando lat_rad e lon_rad
  @spec dist_haversine(Ponto.t(), Ponto.t()) :: float()
  defp dist_haversine(%Ponto{lat_rad: phi1, lon_rad: lambda1}, %Ponto{
         lat_rad: phi2,
         lon_rad: lambda2
       }) do
    dphi = phi2 - phi1
    dlambda = lambda2 - lambda1

    a =
      :math.sin(dphi / 2) ** 2 +
        :math.cos(phi1) * :math.cos(phi2) * :math.sin(dlambda / 2) ** 2

    2 * @raio_terra_km * :math.atan2(:math.sqrt(a), :math.sqrt(1 - a))
  end
end
