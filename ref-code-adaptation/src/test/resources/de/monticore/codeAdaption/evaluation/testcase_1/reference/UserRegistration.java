package de.monticore.codeAdaption.evaluation.reference;

import de.monticore.codeAdaption.evaluation.reference.entity.User;
import de.monticore.codeAdaption.utils.Adapt;


public class UserRegistration {
        @Adapt(ignore = true)

        public void register(User user) {
            // Implement logic to save the user details (username, password, roles) to the database
        }

}
