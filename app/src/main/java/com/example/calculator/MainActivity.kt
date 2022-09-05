package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var canAddOperation = false
    private var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //возможность продолжать использовать операции, если экран перевернулся
        if (textView_calculation.text.isNotEmpty()) {
            canAddOperation = true
        }
    }

    //сохранение данных по ключу
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString("CALCULATION", textView_calculation.text.toString())
            putString("RESULT", textView_result.text.toString())
        }

        super.onSaveInstanceState(outState)
    }

    //восстановление данных по ключу
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        textView_calculation.text = savedInstanceState.getString("CALCULATION")
        textView_result.text = savedInstanceState.getString("RESULT")

        super.onRestoreInstanceState(savedInstanceState)
    }

    //очищение всех данных
    fun clearAll(view: View) {
        view.isClickable = true

        textView_calculation.text = ""
        textView_result.text = ""
        //возможность использовать операции
        canAddOperation = false
        canAddDecimal = true
    }

    //удаление последнего введенного элемента
    fun backspace(view: View) {
        view.isClickable = true

        val length = textView_calculation.length()
        if (length > 0) {
            textView_calculation.text = textView_calculation.text.subSequence(0, length - 1)
        }

        //возможность использовать операции
        canAddOperation = textView_calculation.text.isNotEmpty()
    }

    //использование операций
    fun symbolsOperation(view: View) {
        if (canAddOperation) {
            view.isClickable = true

            val lastItem =
                textView_calculation.text.toString()[textView_calculation.text.length - 1]

            //добавление операции или замена существующей
            if (view is Button) {
                if (lastItem.isDigit() || lastItem == '.') {
                    textView_calculation.append(view.text)
                } else if (!lastItem.isDigit()) {
                    val length = textView_calculation.length()
                    textView_calculation.text = textView_calculation.text.subSequence(0, length - 1)
                    textView_calculation.append(view.text)
                }

                canAddDecimal = true
            }
        }
    }

    //вставка цифр и точки
    fun numberOperation(view: View) {
        if (view is Button) {
            if (view.text == ".") {
                if (canAddDecimal)
                    textView_calculation.append(view.text)

                canAddDecimal = false
            } else
                textView_calculation.append(view.text)

            canAddOperation = true
        }
    }

    //подсчет результата
    @Suppress("CovariantEquals")
    fun equals(view: View) {
        view.isClickable = true
        textView_result.text = calculateResults()
    }

    private fun calculateResults(): String {
        //проход по TextView и запись в MutableList
        val digitsOperators = digitsOperators()
        if (digitsOperators.isEmpty()) return ""

        //выполнение операций * и ÷
        val timesDivision = timesDivisionCalculate(digitsOperators)
        if (timesDivision.isEmpty()) return ""

        //выполнение операций + и -
        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    //проход по TextView и запись в MutableList
    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (character in textView_calculation.text) {
            if (character.isDigit() || character == '.')
                currentDigit += character
            else {
                //если другой символ кроме точки или цифры, то записываем текущее в MutableList
                //если в выражении просто точка, то будет записано как 0.0
                if (currentDigit == ".")
                    currentDigit = 0.0.toString()
                list.add(currentDigit.toFloat())
                currentDigit = ""
                //запись текущего символа (операции)
                list.add(character)
            }
        }

        //последняя проверка на заполненность значений
        if (currentDigit != "") {
            //если в выражении просто точка, то будет записано как 0.0
            if (currentDigit == ".")
                currentDigit = 0.0.toString()
            list.add(currentDigit.toFloat())
        }

        return list
    }

    //проверка MutableList на содержание * и ÷
    private fun timesDivisionCalculate(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while (list.contains('×') || list.contains('÷')) {
            list = calcTimesDiv(list)
        }
        return list
    }

    //выполнение операций * и ÷
    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var listSize = passedList.size

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < listSize) {
                //операция для вычисления
                val operator = passedList[i]
                //2 значения над которыми производятся вычисления
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when (operator) {
                    '×' -> {
                        newList.add(prevDigit * nextDigit)
                        listSize = i + 1
                    }
                    '÷' -> {
                        newList.add(prevDigit / nextDigit)
                        listSize = i + 1
                    }
                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }

            if (i > listSize)
                newList.add(passedList[i])
        }

        return newList
    }

    //выполнение операций + и -
    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for (i in passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex) {
                //операция для вычисления
                val operator = passedList[i]
                //значение над которым производится вычисление
                val nextDigit = passedList[i + 1] as Float
                if (operator == '+')
                    result += nextDigit
                if (operator == '—')
                    result -= nextDigit
            }
        }

        return result
    }
}