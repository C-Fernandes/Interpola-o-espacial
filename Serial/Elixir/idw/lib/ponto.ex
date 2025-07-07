defmodule Idw.Ponto do
  defstruct latitude: 0.0,
            longitude: 0.0,
            temperatura: 0.0,
            lat_rad: 0.0,
            lon_rad: 0.0,
            data: "",
            hora: "",
            pontos_proximos: []

  @spec deg2rad(float()) :: float()
  defp deg2rad(grau), do: grau * :math.pi() / 180.0

  @spec novo(float(), float(), float(), String.t(), String.t()) :: t()
  def novo(latitude, longitude, temperatura, data, hora) do
    lat_r = deg2rad(latitude)
    lon_r = deg2rad(longitude)

    %__MODULE__{
      latitude: latitude,
      longitude: longitude,
      lat_rad: lat_r,
      lon_rad: lon_r,
      temperatura: temperatura,
      data: data,
      hora: hora,
      pontos_proximos: []
    }
  end
end
