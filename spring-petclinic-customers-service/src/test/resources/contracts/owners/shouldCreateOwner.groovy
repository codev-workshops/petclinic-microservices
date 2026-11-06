package contracts.owners

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create an owner when POST /owners"
    request {
        method POST()
        url "/owners"
        headers {
            contentType applicationJson()
        }
        body(
            firstName: $(consumer(regex("[A-Za-z]+")), producer("Jane")),
            lastName: $(consumer(regex("[A-Za-z]+")), producer("Doe")),
            address: $(consumer(regex(".+")), producer("1 Main St.")),
            city: $(consumer(regex(".+")), producer("Springfield")),
            telephone: $(consumer(regex("\\d{1,12}")), producer("6085550000"))
        )
    }
    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body(
            id: 11,
            firstName: "Jane",
            lastName: "Doe",
            address: "1 Main St.",
            city: "Springfield",
            telephone: "6085550000",
            pets: []
        )
    }
}
