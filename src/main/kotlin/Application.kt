import khttp.get
import khttp.put
import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by tauraamui on 12/07/2017.
 */

class Speedbeam(hubIp: String, username: String) {

    val hubIp = hubIp
    val username = username

    fun updateLight(light: Light): Boolean {
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=light.state.toJSON())
        return response.statusCode == 200
    }

    private fun getLightsInfo(): JSONObject {
        return get("http://$hubIp/api/$username/lights").jsonObject
    }

    private fun getLightInfo(lightId: String): JSONObject {
        return get("http://$hubIp/api/$username/lights/$lightId/state").jsonObject
    }

    fun getLight(lightId: String): Light {

        val lightInfoJSON: JSONObject = (getLightsInfo()[lightId] as JSONObject)

        val lightStateJSON = lightInfoJSON["state"] as JSONObject

        val xy = lightStateJSON["xy"] as JSONArray

        val lightState = LightState(xy = Pair(xy[0], xy[1]) as Pair<Double, Double>, ct = lightStateJSON["ct"] as Int, alert = lightStateJSON["alert"].toString(),
                sat = lightStateJSON["sat"] as Int, effect = lightStateJSON["effect"].toString(), brightness = lightStateJSON["bri"] as Int,
                hue = lightStateJSON["hue"] as Int, colorMode = lightStateJSON["colormode"].toString(), reachable = lightStateJSON["reachable"] as Boolean,
                on = lightStateJSON["on"] as Boolean)
        val light = Light(lightId.toInt(), lightInfoJSON["productid"].toString(), lightInfoJSON["modelid"].toString(), lightInfoJSON["manufacturername"].toString(),
                lightInfoJSON["name"].toString(), lightInfoJSON["swconfigid"].toString(), lightInfoJSON["swversion"].toString(),
                lightState, lightInfoJSON["type"].toString(), lightInfoJSON["uniqueid"].toString())
        return light
    }

    fun getLights(): MutableList<Light> {
        val lights = mutableListOf<Light>()
        val lightsInfo = get("http://$hubIp/api/$username/lights").jsonObject
        lightsInfo.keys().forEach { key ->
            lights.add(getLight(key))
        }
        return lights
    }
}

data class Light(var id: Int = -1, var productId: String = "", var modelId: String = "", var manufacturerName: String = "", var name: String = "",
                 var swConfigId: String = "", var swVersion: String = "", var state: LightState = LightState(), var type: String = "", var uniqueId: String = "") {

    fun toJSON(): JSONObject {
        val mappedValues = mapOf("id" to id, "productid" to productId, "modelid" to modelId, "manufacturername" to manufacturerName, "name" to name, "swconfigid" to swConfigId,
                                 "swversion" to swVersion, "state" to state.toJSON(), "type" to type, "uniqueid" to uniqueId)
        return JSONObject(mappedValues)
    }
}

data class LightState(var xy: Pair<Double, Double> = Pair(-1.0, -1.0), var ct: Int = -1, var alert: String = "", var sat: Int = -1, var effect: String = "",
                      var brightness: Int = -1, var hue: Int = -1, var colorMode: String = "", var reachable: Boolean = false, var on: Boolean = false) {
    fun toJSON(): JSONObject {
        val mappedValues = mapOf("xy" to JSONArray(listOf(xy.first, xy.second)), "ct" to ct, "alert" to alert, "sat" to sat, "effect" to effect, "bri" to brightness,
                                 "hue" to hue, "colormode" to colorMode, "reachable" to reachable, "on" to on)
        return JSONObject(mappedValues)
    }
}

fun main(args: Array<String>) {
    val hubIp = "192.168.1.29"
    val username = "XlWkSHzCzszbGOAicOdhaXgKUINoPlM93qzlxekY"

    val speedBeam = Speedbeam(hubIp, username)

    speedBeam.getLights().forEach(::println)

    while (true) {
        Thread.sleep(3000)
        speedBeam.getLights().forEach { light ->
            light.state.on = !light.state.on
            speedBeam.updateLight(light)
        }
    }
}