defmodule IdwApp.MixProject do
  use Mix.Project

  def project do
    [
      app: :idw,
      version: "0.1.0",
      elixir: "~> 1.12",
      start_permanent: Mix.env() == :prod,
      deps: [],
      # se quiser gerar um executável com mix escript.build:
      escript: [main_module: Idw]
    ]
  end

  def application do
    [
      extra_applications: [:logger]
    ]
  end
end
