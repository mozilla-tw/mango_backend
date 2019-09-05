package org.mozilla.msrp.platform.common

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Call
import retrofit2.Response

// to mock generic types, wee need use reified generics
inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)

// Retrofit return Call<?> object per http request. We need to call it's `execute()` method get result synchronously.
// this method ease the extra mocked-Call object and call `execute()` on the caller's behalf. and return the
// OngoingStubbing that the caller is interested in.
inline fun <reified T> onRetrofitExecute(methodCall: Call<T>): OngoingStubbing<Response<T>>? {
    val mockedCall: Call<T> = mock()
    Mockito.`when`(methodCall).thenReturn(mockedCall)
    return Mockito.`when`(mockedCall.execute())
}