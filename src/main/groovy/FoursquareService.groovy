import groovy.json.JsonSlurper
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import java.util.stream.IntStream

class FoursquareService implements APIService{
    private final def CLIENT_SECRET = "fsq3vUjmxgnoFhn/6nTEvSYxsoQw+VKzY9gf5Q6u4c2du0Q="
    private final int RADIUS_1 = 50
    private final int RADIUS_2 = 100
    private final JsonSlurper jsonSlurper = new JsonSlurper()
    private final def scanner = new Scanner(System.in)

    FoursquareService() {}

    @Override
    def findPlace(latitude, longitude) {
        def result = findLocationAdditional(CLIENT_SECRET, latitude, longitude, RADIUS_1)

        if (result.isEmpty()) {
            result = findLocationAdditional(CLIENT_SECRET, latitude, longitude, RADIUS_2)
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

//        def checkAverage = IntStream.of(result["distance"] as int[]).max().getAsInt().compareTo(1,5 * averageDistance)

        def places = new ArrayList<>()

        for (def d : result) {
            //Отбрасываем всех, кто находится дальше средней дистанции, умноженной на 1,5
            if (d["distance"] <= (1.5 * averageDistance)) {
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

        //Если всего 1 место, то сразу возвращаем его
        if (places.size() == 1) return places[0]["name"] + parent

        return "Your location is: " + selectCategory(places) + parent
    }

    //метод для получения списка локаций по заданому радиусу и координатам
    private def findLocationAdditional(String clientSecret, latitude, longitude, radius) {
        def url = "https://api.foursquare.com/v3/places/search?ll=${latitude},${longitude}&radius=${radius}&limit=40&fields=name,related_places,categories,distance"

        def request = new HttpGet(url)
        request.addHeader("accept", "application/json")
        request.addHeader("Authorization", clientSecret)

        def response = new DefaultHttpClient().execute(request)
        //Проверка на то, прошел ли запрос
        if (response.getStatusLine().getStatusCode() == 200) {
            def result = jsonSlurper.parse(response.entity.content)["results"]
            return result
        } else {
            throw new RuntimeException("Something went wrong with connection to FoursquareApi. Try again later!")
        }
    }

    //Метод, который позволяет выбрать соответствующую категорию, для уточнения места
    private def selectCategory(places) {
        def uniqueCategories = places.collect {it["categories"]["name"][0]} as Set
        if (uniqueCategories.size() == 1) return selectPlace(places["name"])

        while (true) {
            println "Select category of your location:"
            uniqueCategories.each(s -> println " * " + s)

            def category = scanner.nextLine()

            //Когда мы определились с категорией, собираем все локации, которые в ней находятся
            def listOfPlacesInUserCategory = places.findAll { it["categories"]["name"][0] == category }.collect {it["name"]}

            if (listOfPlacesInUserCategory.size() == 0)
                println "Choose the right category!"
            else if (listOfPlacesInUserCategory.size() == 1)
                return listOfPlacesInUserCategory[0]["name"]
            else {
                println "There are different locations in this category"
                return selectPlace(listOfPlacesInUserCategory)
            }
        }
    }

    //Метод для нахождения конкретного места
    private def selectPlace(listOfPlacesInUserCategory) {
        while (true) {
            println "Please, select your location:"
            listOfPlacesInUserCategory.each(s -> println " * " + s)

            def name = scanner.nextLine()

            for (def p : listOfPlacesInUserCategory) {
                if (p == name)
                    return p
            }

            println "Choose the right category!"
        }
    }
}
