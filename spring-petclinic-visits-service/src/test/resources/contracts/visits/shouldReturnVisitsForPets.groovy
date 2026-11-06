package contracts.visits

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return the visits of the given pets when GET /pets/visits?petId={ids}"
    request {
        method GET()
        urlPath("/pets/visits") {
            queryParameters {
                parameter "petId": $(consumer(matching("[0-9]+(,[0-9]+)*")), producer("1,2"))
            }
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            items: [
                [id: 1, petId: 1, date: "2013-01-01", description: "rabies shot"],
                [id: 2, petId: 2, date: "2013-01-02", description: "neutered"]
            ]
        )
    }
}
