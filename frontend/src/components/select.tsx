import * as React from "react"

import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectLabel,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {useEffect, useRef, useState} from "react";
import api from "@/lib/axios";

export function SelectDemo({city, setCity}: {city: string, setCity: (value: string) => void }) {

    const [cities, setCities] = useState<City[]>([])
    const hasFetched = useRef(false)

    // TODO: дергать города надо
    useEffect(() => {
        if (hasFetched.current) return
        api.get("/city").then((res) => setCities(res.data))
            .catch(error => console.log(error))
    }, []);

    return (
        <Select value={city} onValueChange={setCity}>
            <SelectTrigger className="w-[255px]">
                <SelectValue placeholder="Город" />
            </SelectTrigger>
            <SelectContent>
                <SelectGroup>
                    <SelectLabel>Города</SelectLabel>
                    {cities.length > 0 ? (
                        cities.map((city) => (
                            <SelectItem key={city.name} value={city.name}>
                                {city.name}
                            </SelectItem>
                        ))
                    ) : (
                        <div className="px-3 py-2 text-sm text-muted-foreground">
                            Нет доступных городов
                        </div>
                    )}
                </SelectGroup>
            </SelectContent>
        </Select>
    )
}
