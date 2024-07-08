package com.wladkoshelev.metronome.utils.arch

import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class ExampleBaseArchClass {

    fun params() = parametersOf()

    fun mModule() = module {
        factory<Face> {
            Impl(

            )
        }
    }

    interface Face

    class Impl : Face {

    }
}