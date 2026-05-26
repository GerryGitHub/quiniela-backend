package com.quiniela.app.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.quiniela.app.R

object CountryFlagResolver {

    private val flagMap = mapOf(
        "MÉXICO" to R.drawable.mex,
        "SUDÁFICA" to R.drawable.rsa,
        "REPÚBLICA DE COREA" to R.drawable.kor,
        "REPÚBLICA CHECA" to R.drawable.cze,
        "CANADÁ" to R.drawable.can,
        "BOSNIA Y HERZEGOVINA" to R.drawable.bih,
        "CATAR" to R.drawable.qat,
        "SUIZA" to R.drawable.sui,
        "ESTADOS UNIDOS" to R.drawable.usa,
        "PARAGUAY" to R.drawable.par,
        "AUSTRALIA" to R.drawable.aus,
        "TURQUÍA" to R.drawable.tur,
        "BRASIL" to R.drawable.bra,
        "MARRUECOS" to R.drawable.mar,
        "HAITÍ" to R.drawable.hai,
        "ESCOCIA" to R.drawable.sco,
        "ALEMANIA" to R.drawable.ger,
        "CURAZAO" to R.drawable.cuw,
        "PAÍSES BAJOS" to R.drawable.ned,
        "JAPÓN" to R.drawable.jpn,
        "COSTA DE MARFIL" to R.drawable.civ,
        "ECUADOR" to R.drawable.ecu,
        "SUECIA" to R.drawable.swe,
        "TÚNEZ" to R.drawable.tun,
        "ESPAÑA" to R.drawable.esp,
        "CABO VERDE" to R.drawable.cpv,
        "BÉLGICA" to R.drawable.bel,
        "EGIPTO" to R.drawable.egy,
        "ARABIA SAUDÍ" to R.drawable.ksa,
        "URUGUAY" to R.drawable.uru,
        "RI DE IRÁN" to R.drawable.irn,
        "NUEVA ZELANDA" to R.drawable.nzl,
        "FRANCIA" to R.drawable.fra,
        "SENEGAL" to R.drawable.sen,
        "IRAK" to R.drawable.irq,
        "NORUEGA" to R.drawable.nor,
        "ARGENTINA" to R.drawable.arg,
        "ARGELIA" to R.drawable.alg,
        "AUSTRIA" to R.drawable.aut,
        "JORDANIA" to R.drawable.jor,
        "PORTUGAL" to R.drawable.por,
        "RD CONGO" to R.drawable.cod,
        "INGLATERRA" to R.drawable.eng,
        "CROACIA" to R.drawable.cro,
        "GHANA" to R.drawable.gha,
        "PANAMÁ" to R.drawable.pan,
        "UZBEKISTÁN" to R.drawable.uzb,
        "COLOMBIA" to R.drawable.col,
    )

    fun getFlagResource(countryName: String?): Int {
        if (countryName.isNullOrBlank()) return R.drawable.mex
        val key = countryName.uppercase().trim()
        return flagMap[key] ?: R.drawable.mex
    }

    fun getFlagDrawable(context: Context, countryName: String?): Drawable? {
        val resId = getFlagResource(countryName)
        return AppCompatResources.getDrawable(context, resId)
    }
}
