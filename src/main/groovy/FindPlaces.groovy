import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import java.util.stream.IntStream

//метод для получения списка локаций по заданому радиусу и координатам
static def findLocationAdditional(String clientSecret, latitude, longitude, radius) {
    def url = "https://api.foursquare.com/v3/places/search?ll=${latitude},${longitude}&radius=${radius}&limit=40&fields=name,related_places,categories,distance"

    def request = new HttpGet(url)
    request.addHeader("accept", "application/json")
    request.addHeader("Authorization", clientSecret)

    def response = new DefaultHttpClient().execute(request)
    def result = new JsonSlurper().parse(response.entity.content)["results"]

    return result
}

static def findPlace(latitude, longitude) {
    def clientSecret = "fsq3vUjmxgnoFhn/6nTEvSYxsoQw+VKzY9gf5Q6u4c2du0Q="

    def result = findLocationAdditional(clientSecret, latitude, longitude, 30)

    if (result == null) {
        result = findLocationAdditional(clientSecret, latitude, longitude, 60)
        if (result == null)
            return "Couldn't find any places nearby"
    }

    //Считаем среднюю дистанцию от заданной точки (чтобы отбросить самые дальние локации)
    def averageDistance = IntStream.of(result["distance"] as int[]).average().orElse(0)
    //Берем самую ближнюю локацию и получаем ее пэрента
    def parent = result[0]["related_places"]["parent"]["name"]
    def places = new ArrayList<>()
    println averageDistance

    for (def d : result) {
        //Отбрасываем всех, кто находится дальше средней дистанции
        if (d["distance"] <= averageDistance) {
            //Если пэерент у ближайшей локации есть, тогда добавляем места только с пэрентами
            if (parent != null) {
                if (d["related_places"]["parent"] != null) {
                    places.add(d)
                }
                //Если у ближайшей локации нет пэрента, тогда возвращаем ближайший элемент
            } else {
                return "${result[0]["name"]}"
            }
        }
    }

    return selectPlace(places) + " parent: " + parent
}

//Метод, который позволяет выбрать соответствующую категорию, для уточнения места
static def selectPlace(places) {
    def scanner = new Scanner(System.in)

    while (true) {
        println "Select category of your location:"
        places.forEach(s -> println " * " + s["categories"]["name"][0])
        def category = scanner.nextLine()

        for (def p : places) {
            if (category == p["categories"]["name"][0])
                return p["name"]
        }

        println "Choose the right category!"
    }
}