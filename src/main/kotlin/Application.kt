import khttp.get
import khttp.put
import khttp.responses.Response
import org.json.JSONObject

/**
 * Created by tauraamui on 12/07/2017.
 */

class Speedbeam(hubIp: String, username: String) {

    val hubIp = hubIp
    val username = username

    fun isLightOn(light: Light): Boolean {
        val lightInfo = getLightsInfo()
        return false
    }

    fun turnLightOn(): Boolean {
        val payload = mapOf("on" to true)
        val response = put("http://$hubIp/api/$username/lights/4/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    fun turnLightOn(light: Light): Boolean {
        val payload = mapOf("on" to true)
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    fun turnLightOff(): Boolean {
        val payload = mapOf("on" to false)
        val response = put("http://$hubIp/api/$username/lights/4/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    fun turnLightOff(light: Light): Boolean {
        val payload = mapOf("on" to false)
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    private fun getLightsInfo(): JSONObject {
        return get("http://$hubIp/api/$username/lights").jsonObject
    }

    private fun getLightInfo(lightId: Int): JSONObject {
        return get("http://$hubIp/api/$username/lights/${lightId.toInt()}/state").jsonObject
    }

    fun getLights(): MutableList<Light> {
        val lights = mutableListOf<Light>()
        val lightsInfo = get("http://$hubIp/api/$username/lights").jsonObject
        lightsInfo.keys().forEach { key ->
            val lightInfo: JSONObject = lightsInfo[key] as JSONObject
            val lightStateJSON: JSONObject = lightInfo["state"] as JSONObject
            val lightState = LightState(on = lightStateJSON["on"] as Boolean)
            val light = Light(key.toInt(), lightInfo["productid"].toString(), lightInfo["modelid"].toString(), lightInfo["manufacturername"].toString(),
                                lightInfo["name"].toString(), lightInfo["swconfigid"].toString(), lightInfo["swversion"].toString(),
                                    lightState, lightInfo["type"].toString(), lightInfo["uniqueid"].toString())
            lights.add(light)
        }
        return lights
    }
}

data class Light(var id: Int = -1, var productId: String = "", var modelId: String = "", var manufacturerName: String = "", var name: String = "",
                 var swConfigId: String = "", var swVersion: String = "", var state: LightState = LightState(), var type: String = "", var uniqueId: String = "")

data class LightState(var xy: Pair<Float, Float> = Pair(-1F, -1F), var ct: Int = -1, var alert: String = "", var sat: Int = -1, var effect: String = "",
                      var brightness: Int = -1, var hue: Int = -1, var colorMode: String = "", var reachable: Boolean = false, var on: Boolean = false)

fun main(args: Array<String>) {
    val hubIp = "192.168.1.29"
    val username = "XlWkSHzCzszbGOAicOdhaXgKUINoPlM93qzlxekY"

    val speedBeam = Speedbeam(hubIp, username)

    speedBeam.getLights().forEach { light ->
        if (!light.state.on) speedBeam.turnLightOn(light)
        else if (light.state.on) speedBeam.turnLightOff(light)
    }
}