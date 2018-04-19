package com.example.subayyal.dailynotes.Exceptions;

import com.amazonaws.AmazonClientException;

/**
 * Created by subayyal on 4/4/2018.
 */

public class CreateUserRemoteException extends AmazonClientException {
    public CreateUserRemoteException(String message) {
        super(message);
    }
}
