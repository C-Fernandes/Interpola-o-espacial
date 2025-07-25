defmodule Idw.Application do
  @moduledoc false

  use Application

  @impl true
  def start(_type, _args) do
    Idw.main()

    children = []

    opts = [strategy: :one_for_one, name: Idw.Supervisor]
    Supervisor.start_link(children, opts)
  end
end
