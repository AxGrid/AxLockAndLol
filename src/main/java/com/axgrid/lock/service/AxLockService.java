package com.axgrid.lock.service;

import com.axgrid.lock.repository.AxLockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AxLockService {

    @Autowired
    AxLockRepository lockRepository;



}
