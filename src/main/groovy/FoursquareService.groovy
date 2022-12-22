import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import java.util.stream.IntStream

class FoursquareService implements APIService{
    private final def CLIENT_SECRET = "fsq3vUjmxgnoFhn/6nTEvSYxsoQw+VKzY9gf5Q6u4c2du0Q="

    FoursquareService() {}

    @Override
    def findPlace(latitude, longitude) {
        def result = findLocationAdditional(CLIENT_SECRET, latitude, longitude, 50)

        if (result.isEmpty()) {
            result = findLocationAdditional(CLIENT_SECRET, latitude, longitude, 100)
            if (result.isEmpty())
                return "Couldn't find any places nearby"
        }

        //Считаем среднюю дистанцию от заданной точки (чтобы отбросить самые дальние локации)
        def averageDistance = IntStream.of(result["distance"] as int[]).average().orElse(0)
        //Берем ближайшую локацию и получаем ее пэрента
        def parent
        try {
            parent = " at " + result[0]["related_places"]["parent"]["name"] as String
        } catch (NullPointerException e) {
            parent = null
        }

        def places = new ArrayList<>()

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
                    return "Your location is: ${result[0]["name"]}"
                }
            }
        }

        return "Your location is: " + selectPlace(places) + parent
    }

    //метод для получения списка локаций по заданому радиусу и координатам
    private def findLocationAdditional(String clientSecret, latitude, longitude, radius) {
        def url = "https://api.foursquare.com/v3/places/search?ll=${latitude},${longitude}&radius=${radius}&limit=40&fields=name,related_places,categories,distance"

        def request = new HttpGet(url)
        request.addHeader("accept", "application/json")
        request.addHeader("Authorization", clientSecret)

        def response = new DefaultHttpClient().execute(request)
        def result = new JsonSlurper().parse(response.entity.content)["results"]

        return result
    }

    //Метод, который позволяет выбрать соответствующую категорию, для уточнения места
    private def selectPlace(places) {
        def scanner = new Scanner(System.in)

        while (true) {
            println "Select category of your location:"
            def uniqueCategories = places.collect {it["categories"]["name"][0]} as Set
            uniqueCategories.each(s -> println " * " + s)

            def category = scanner.nextLine()

            for (def p : places) {
                if (category == p["categories"]["name"][0])
                    return p["name"]
            }

            println "Choose the right category!"
        }
    }
}
