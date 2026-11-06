package contracts.owners

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return the owner with its pets when GET /owners/{ownerId}"
    request {
        method GET()
        url "/owners/1"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            id: 1,
            firstName: "George",
            lastName: "Franklin",
            address: "110 W. Liberty St.",
            city: "Madison",
            telephone: "6085551023",
            pets: [
                [
                    id       : 1,
                    name     : "Leo",
                    birthDate: $(consumer("2010-09-07"), producer(regex("\\d{4}-\\d{2}-\\d{2}.*"))),
                    type     : [id: 1, name: "cat"]
                ]
            ]
        )
    }
}
