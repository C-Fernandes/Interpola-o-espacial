# lib/microbenchmark/idw_benchs.exs

alias Idw.InversoDistanciaPonderada, as: IDW
alias Idw.LeitorCSV
alias Idw.Ponto
alias IDWState

# 1) Gera “estado” (lista de 10_000 pontos + ponto_alvo)
%{lista_candidatos: lista, ponto_alvo: alvo_original} = IDWState.gerar_estado()

# Parâmetros do IDW
k = 5
p = 2

Benchee.run(
  %{
    "distancia_geografica" => fn ->
      Enum.reduce(lista, 0.0, fn ponto, acc ->
        d = IDW.distancia_geografica(alvo_original, ponto)
        acc + d
      end)
    end,
    "atualizar_vizinhos" => fn ->
      IDW.vizinhos_mais_proximos(alvo_original, lista, k)
      |> length()
    end,
    "interpolar_com_vizinhos" => fn ->
      alvo_clone =
        Ponto.novo(
          alvo_original.latitude,
          alvo_original.longitude,
          -9999.0,
          alvo_original.data,
          alvo_original.hora
        )

      vizinhos = IDW.vizinhos_mais_proximos(alvo_clone, lista, k)
      IDW.interpolar_com_vizinhos(alvo_clone, vizinhos, p)
    end,
    "processar_lote" => fn ->
      alvo_clone =
        Ponto.novo(
          alvo_original.latitude,
          alvo_original.longitude,
          -9999.0,
          alvo_original.data,
          alvo_original.hora
        )

      candidatos = LeitorCSV.filtrar_por_hora(lista, alvo_clone.hora)
      vizinhos = IDW.vizinhos_mais_proximos(alvo_clone, candidatos, k)
      IDW.interpolar_com_vizinhos(alvo_clone, vizinhos, p)
    end
  },
  time: 5,
  warmup: 2,
  memory_time: 0,
  formatters: [
    {Benchee.Formatters.Console, time: :millisecond}
  ]
)
