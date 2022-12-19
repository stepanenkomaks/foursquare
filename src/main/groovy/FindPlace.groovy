import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

static def findPlace(latitude, longitude) {
    def clientSecret = "fsq3vUjmxgnoFhn/6nTEvSYxsoQw+VKzY9gf5Q6u4c2du0Q="

    def url = "https://api.foursquare.com/v3/places/search?ll=${latitude},${longitude}&radius=25&limit=1&fields=name,related_places"

    def request = new HttpGet(url)
    request.addHeader("accept", "application/json")
    request.addHeader("Authorization", clientSecret)

    def response = new DefaultHttpClient().execute(request)
    def result = new JsonSlurper().parse(response.entity.content)
    result

    return result["results"]["name"] + result["results"]["related_places"]["parent"]["name"]
}
