package contracts.pets

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should add a pet to an owner when POST /owners/{ownerId}/pets"
    request {
        method POST()
        url "/owners/1/pets"
        headers {
            contentType applicationJson()
        }
        body(
            id: $(consumer(regex("\\d+")), producer(0)),
            name: $(consumer(regex(".+")), producer("Max")),
            birthDate: $(consumer(regex("\\d{4}-\\d{2}-\\d{2}")), producer("2021-04-01")),
            typeId: $(consumer(regex("\\d+")), producer(2))
        )
    }
    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body(
            id: 14,
            name: "Max",
            birthDate: $(consumer("2021-04-01"), producer(regex("2021-04-01.*"))),
            type: [id: 2, name: "dog"]
        )
    }
}
