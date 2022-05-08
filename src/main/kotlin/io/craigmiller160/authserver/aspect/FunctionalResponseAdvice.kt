package io.craigmiller160.authserver.aspect

import arrow.core.Either
import arrow.core.getOrHandle
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class FunctionalResponseAdvice {
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)") fun postMapping() {}
  @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)") fun getMapping() {}
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)") fun putMapping() {}
  @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
  fun deleteMapping() {}
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
  fun patchMapping() {}

  @Around("postMapping() || getMapping() || putMapping() || deleteMapping() || patchMapping()")
  fun attempt(joinPoint: ProceedingJoinPoint): Any? =
    when (val result = joinPoint.proceed()) {
      is Either<*, *> -> handleEither(result)
      else -> result
    }

  private fun handleEither(either: Either<*, *>): Any? =
    either.getOrHandle { left ->
      when (left) {
        is Throwable -> throw left
        else -> left
      }
    }
}
