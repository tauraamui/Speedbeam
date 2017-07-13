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

    fun turnLightOn(light: Light): Boolean {
        val payload = mapOf("on" to true)
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    fun turnLightOff(light: Light): Boolean {
        val payload = mapOf("on" to false)
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=JSONObject(payload))
        return response.statusCode == 200
    }

    fun updateLight(light: Light): Boolean {
        val response = put("http://$hubIp/api/$username/lights/${light.id}/state", data=JSONObject(light.toJSON()))
        return response.statusCode == 200
    }

    private fun getLightsInfo(): JSONObject {
        return get("http://$hubIp/api/$username/lights").jsonObject
    }

    private fun getLightInfo(lightId: Int): JSONObject {
        return get("http://$hubIp/api/$username/lights/${lightId.toInt()}/state").jsonObject
    }

    fun getLight(lightId: String): Light {

        val lightInfoJSON: JSONObject = (getLightsInfo()[lightId] as JSONObject)

        val lightStateJSON = lightInfoJSON["state"] as JSONObject

        val xy = lightStateJSON["xy"] as JSONArray

        val lightState = LightState(xy = Pair(xy[0], xy[1]) as Pair<Float, Float>, ct = lightStateJSON["ct"] as Int, alert = lightStateJSON["alert"].toString(),
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

data class LightState(var xy: Pair<Float, Float> = Pair(-1F, -1F), var ct: Int = -1, var alert: String = "", var sat: Int = -1, var effect: String = "",
                      var brightness: Int = -1, var hue: Int = -1, var colorMode: String = "", var reachable: Boolean = false, var on: Boolean = false) {
    fun toJSON() {
        //val mappedValues =
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