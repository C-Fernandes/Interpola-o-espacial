defmodule Idw.Ponto do
  defstruct latitude: 0.0,
            longitude: 0.0,
            temperatura: 0.0,
            lat_rad: 0.0,
            lon_rad: 0.0,
            data: "",
            hora: "",
            pontos_proximos: []

  @type t :: %__MODULE__{
          latitude: float(),
          longitude: float(),
          temperatura: float(),
          lat_rad: float(),
          lon_rad: float(),
          data: String.t(),
          hora: String.t(),
          pontos_proximos: list()
        }

  @spec deg2rad(float()) :: float()
  defp deg2rad(grau), do: grau * :math.pi() / 180.0

  @doc """
  Cria um %Ponto{} a partir de latitude/longitude em graus,
  já preenchendo lat_rad e lon_rad.
  """
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
