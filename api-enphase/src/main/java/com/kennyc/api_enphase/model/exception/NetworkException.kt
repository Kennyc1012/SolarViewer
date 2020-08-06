package com.kennyc.api_enphase.model.exception

import okio.IOException

class NetworkException(val code: Int) : IOException()