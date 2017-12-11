package com.br.esoterics.esoadmin.utils

import com.br.esoterics.esoadmin.Center
import com.google.android.gms.maps.model.MarkerOptions


class FilterManager {

    fun filterCenterMarkersByType(filter: String,
                                  listOfMarkersOptions: ArrayList<MarkerOptions>,
                                  listOfCenters: ArrayList<Center>
                                            ): ArrayList<MarkerOptions>{

        var markerOptionsFiltered = ArrayList<MarkerOptions>()
            if(filter.equals("Todos")){
                return listOfMarkersOptions
            }else{
                listOfMarkersOptions.forEach { marker ->
                    listOfCenters.forEach { center ->
                        if (center.getKey().equals(marker.snippet) && center.getModel().type.equals(filter)){
                            markerOptionsFiltered.add(marker)
                        }
                    }
                }
                return markerOptionsFiltered
            }
    }


}
