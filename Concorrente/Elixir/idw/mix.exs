defmodule Idw.MixProject do
  use Mix.Project

  def project do
    [
      app: :idw,
      version: "0.1.0",
      elixir: "~> 1.18",
      start_permanent: Mix.env() == :prod,
      deps: deps()
    ]
  end

  # Run "mix help compile.app" to learn about applications.
  def application do
    [
      extra_applications: [:logger],
      mod: {Idw.Application, []}
    ]
  end

  # Run "mix help deps" to learn about dependencies.
  defp deps do
    [
      {:nimble_csv, "~> 1.2"},
      # Use only: :dev se for apenas para desenvolvimento/benchmarks
      {:flow, "~> 1.2"},
      # Adicione meck
      {:benchee, "~> 1.0", only: :dev},
      {:meck, "~> 0.8", only: :test, runtime: false}

      # {:dep_from_hexpm, "~> 0.3.0"},
      # {:dep_from_git, git: "https://github.com/elixir-lang/my_dep.git", tag: "0.1.0"}
    ]
  end
end
