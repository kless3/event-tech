package com.ems.userservice.exception

class EmailAlreadyExistsException(email: String) : RuntimeException("User with email '$email' already exists")
