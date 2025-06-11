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

export function SelectCity({city, setCity}: {city: string, setCity: (value: string) => void }) {

    const [cities, setCities] = useState<string[]>([])
    const hasFetched = useRef(false)

    useEffect(() => {
        if (hasFetched.current) return
        api.get("/v1/dashboard/city").then((res) => {
            setCities(res.data)
        })
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
                    {
                        cities.length > 0 ? (
                        cities.map((city) => (
                            <SelectItem key={city} value={city}>
                                {city}
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
