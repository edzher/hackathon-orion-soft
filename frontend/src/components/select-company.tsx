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

export function SelectCompany({company, setCompany}: {company: string, setCompany: (value: string) => void }) {

    const [companies, setCompanies] = useState<string[]>([]);
    const hasFetched = useRef(false)

    useEffect(() => {
        if (hasFetched.current) return
        api.get("/v1/dashboard/company").then((res) => {
            setCompanies(res.data)
        })
            .catch(error => console.log(error))
    }, []);

    return (
        <Select value={company} onValueChange={setCompany}>
            <SelectTrigger className="w-[255px]">
                <SelectValue placeholder="Компания" />
            </SelectTrigger>
            <SelectContent>
                <SelectGroup>
                    <SelectLabel>Города</SelectLabel>
                    {companies.length > 0 ? (
                        companies.filter(line => line.trim() !== "")
                            .map((company) => (
                            <SelectItem key={company} value={company}>
                                {company}
                            </SelectItem>
                        ))
                    ) : (
                        <div className="px-3 py-2 text-sm text-muted-foreground">
                            Нет доступных компаний
                        </div>
                    )}
                </SelectGroup>
            </SelectContent>
        </Select>
    )
}
