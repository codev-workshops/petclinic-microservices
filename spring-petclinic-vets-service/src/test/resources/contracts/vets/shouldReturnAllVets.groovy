package contracts.vets

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return all vets with their specialties when GET /vets"
    request {
        method GET()
        url "/vets"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [id: 1, firstName: "James", lastName: "Carter", specialties: [[id: 2, name: "surgery"]], nrOfSpecialties: 1],
            [id: 2, firstName: "Helen", lastName: "Leary", specialties: [[id: 1, name: "radiology"]], nrOfSpecialties: 1]
        ])
    }
}
