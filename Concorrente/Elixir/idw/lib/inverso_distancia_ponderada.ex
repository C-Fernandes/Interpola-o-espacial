defmodule Idw.InversoDistanciaPonderada do
  alias Idw.Ponto
  @raio_terra_km 6371.0
  def atualizar_vizinhos_mais_proximos(alvo, candidatos, k) do
    heap =
      Enum.reduce(candidatos, [], fn candidato, acc ->
        acc = [candidato | acc]

        if length(acc) > k do
          Enum.sort_by(acc, fn p ->
            -distancia_geografica(alvo.latitude, alvo.longitude, p.latitude, p.longitude)
          end)
          |> Enum.take(k)
        else
          acc
        end
      end)

    %Ponto{alvo | pontos_proximos: heap}
  end

  def interpolar_com_vizinhos(alvo, p) do
    eps = 1.0e-9

    iguais =
      Enum.filter(alvo.pontos_proximos, fn viz ->
        distancia_geografica(alvo.latitude, alvo.longitude, viz.latitude, viz.longitude) < eps
      end)

    if length(iguais) > 0 do
      Enum.map(iguais, & &1.temperatura)
      |> Enum.sum()
      |> Kernel./(length(iguais))
    else
      Enum.reduce(alvo.pontos_proximos, {0.0, 0.0}, fn viz, {num, den} ->
        d = distancia_geografica(alvo.latitude, alvo.longitude, viz.latitude, viz.longitude)
        peso = 1.0 / :math.pow(d, p)
        {num + peso * viz.temperatura, den + peso}
      end)
      |> (fn {num, den} -> num / den end).()
    end
  end

  def distancia_geografica(lat1, lon1, lat2, lon2) do
    r = 6371.0
    dlat = :math.pi() * (lat2 - lat1) / 180.0
    dlon = :math.pi() * (lon2 - lon1) / 180.0

    a =
      :math.sin(dlat / 2) * :math.sin(dlat / 2) +
        :math.cos(:math.pi() * lat1 / 180.0) * :math.cos(:math.pi() * lat2 / 180.0) *
          :math.sin(dlon / 2) * :math.sin(dlon / 2)

    r * 2 * :math.atan2(:math.sqrt(a), :math.sqrt(1 - a))
  end
end
