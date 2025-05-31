defmodule IdwTest do
  use ExUnit.Case
  doctest Idw

  test "greets the world" do
    assert Idw.hello() == :world
  end
end
