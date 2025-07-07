defmodule IdwApp.MixProject do
  use Mix.Project

  def project do
    [
      app: :idw,
      version: "0.1.0",
      elixir: "~> 1.12",
      start_permanent: Mix.env() == :prod,
      # ← aqui
      deps: deps(),
      escript: [main_module: Idw]
    ]
  end

  def application do
    [
      extra_applications: [:logger]
    ]
  end

  defp deps do
    [
      {:benchee, "~> 1.1", only: :dev}
      # outras deps…
    ]
  end
end
