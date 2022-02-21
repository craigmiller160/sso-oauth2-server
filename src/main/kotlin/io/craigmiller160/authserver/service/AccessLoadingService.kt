package io.craigmiller160.authserver.service

import org.springframework.stereotype.Service

@Service
class AccessLoadingService {

    // TODO consider what the access level should be?
    fun getAccessForUser(userId: Long) {
        /*
         * 1) Get all ClientUsers for User
         * 2) Get all ClientUserRoles for User
         * 3) Get all Clients for User
         * 4) Get all Roles for Client & User
         */
        TODO("Finish this")
    }

}