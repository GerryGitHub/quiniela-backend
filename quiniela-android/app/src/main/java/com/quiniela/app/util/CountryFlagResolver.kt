package com.quiniela.app.util

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources

object CountryFlagResolver {

    private val flagMap = mapOf(
        "MEXICO" to R.drawable.mex,
        "MÉXICO" to R.drawable.mex,
        "SUDAFRICA" to R.drawable.rsa,
        "COREA DEL SUR" to R.drawable.kor,
        "REPUBLICA DE COREA" to R.drawable.kor,
        "CHEQUIA" to R.drawable.cze,
        "REPUBLICA CHECA" to R.drawable.cze,
        "CANADA" to R.drawable.can,
        "CANADÁ" to R.drawable.can,
        "BOSNIA Y HEGOVINA" to R.drawable.bih,
        "BOSNIA Y HERZEGOVINA" to R.drawable.bih,
        "BOSNIA" to R.drawable.bih,
        "QATAR" to R.drawable.qat,
        "SUIZA" to R.drawable.sui,
        "BRASIL" to R.drawable.bra,
        "MARRUECOS" to R.drawable.mar,
        "HAITI" to R.drawable.hai,
        "HAITÍ" to R.drawable.hai,
        "ESCOCIA" to R.drawable.sco,
        "ESTADOS UNIDOS" to R.drawable.usa,
        "USA" to R.drawable.usa,
        "EE.UU." to R.drawable.usa,
        "PARAGUAY" to R.drawable.par,
        "AUSTRALIA" to R.drawable.aus,
        "TURQUIA" to R.drawable.tur,
        "TURQUÍA" to R.drawable.tur,
        "ALEMANIA" to R.drawable.ger,
        "CURAZAO" to R.drawable.cuw,
        "COSTA DE MARFIL" to R.drawable.civ,
        "ECUADOR" to R.drawable.ecu,
        "PAISES BAJOS" to R.drawable.ned,
        "PAÍSES BAJOS" to R.drawable.ned,
        "HOLANDA" to R.drawable.ned,
        "JAPON" to R.drawable.jpn,
        "JAPÓN" to R.drawable.jpn,
        "SUECIA" to R.drawable.swe,
        "TUNEZ" to R.drawable.tun,
        "TÚNEZ" to R.drawable.tun,
        "BELGICA" to R.drawable.bel,
        "BÉLGICA" to R.drawable.bel,
        "EGIPTO" to R.drawable.egy,
        "IRAN" to R.drawable.irn,
        "IRÁN" to R.drawable.irn,
        "NUEVA ZELANDA" to R.drawable.nzl,
        "ESPAÑA" to R.drawable.esp,
        "ESPANA" to R.drawable.esp,
        "CABO VERDE" to R.drawable.cpv,
        "ARABIA SAUDITA" to R.drawable.ksa,
        "URUGUAY" to R.drawable.uru,
        "FRANCIA" to R.drawable.fra,
        "SENEGAL" to R.drawable.sen,
        "IRAQ" to R.drawable.irq,
        "NORUEGA" to R.drawable.nor,
        "ARGENTINA" to R.drawable.arg,
        "ARGELIA" to R.drawable.alg,
        "AUSTRIA" to R.drawable.aut,
        "JORDANIA" to R.drawable.jor,
        "PORTUGAL" to R.drawable.por,
        "REPUBLICA DEMOCRATICA DEL CONGO" to R.drawable.cod,
        "REPÚBLICA DEMOCRÁTICA DEL CONGO" to R.drawable.cod,
        "CONGO" to R.drawable.cod,
        "UZBEKISTAN" to R.drawable.uzb,
        "UZBEKISTÁN" to R.drawable.uzb,
        "COLOMBIA" to R.drawable.col,
        "INGLATERRA" to R.drawable.eng,
        "CROACIA" to R.drawable.cro,
        "GHANA" to R.drawable.gha,
        "PANAMA" to R.drawable.pan,
        "PANAMÁ" to R.drawable.pan,
    )

    fun getFlagResource(countryName: String?): Int {
        if (countryName.isNullOrBlank()) return R.drawable.mex
        val key = countryName.uppercase().trim()
        return flagMap[key] ?: R.drawable.mex
    }

    fun getFlagDrawable(context: Context, countryName: String?): android.graphics.drawable.Drawable? {
        val resId = getFlagResource(countryName)
        return AppCompatResources.getDrawable(context, resId)
    }
}
