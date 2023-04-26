package proton.android.pass.common.api

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(
        onSuccess = { transform(it) },
        onFailure = { Result.failure(it) }
    )

fun <T> List<Result<T>>.firstError(): Throwable? = firstOrNull { it.isFailure }?.exceptionOrNull()

fun <T> List<Result<T>>.transpose(): Result<List<T>> {
    val error = this.firstError()
    if (error != null) {
        return Result.failure(error)
    }

    val allValues = this.map { result ->
        result.fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )
    }

    return Result.success(allValues)
}

