'use client'

import {IconRefresh} from "@tabler/icons-react"

import { Button } from "@/components/ui/button"
import {
    SidebarGroup,
    SidebarGroupContent,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
} from "@/components/ui/sidebar"
import {Input} from "@/components/ui/input";
import {SelectDemo} from "@/components/select";
import {DatePicker} from "@/components/date-picker";
import {Label} from "@/components/ui/label";
import * as React from "react";
import {useState} from "react";
import { toast } from "sonner"
import {useFilters} from "@/context/filters";

export function Filters() {

    const {setFilters} = useFilters()

    const [vacancy, setVacancy] = useState<string>("");
    const [experience, setExperience] = useState<string>("");
    const [city, setCity] = useState<string>("");
    const [minSalary, setMinSalary] = useState<string>("");
    const [maxSalary, setMaxSalary] = useState<string>("");
    const [startDate, setStartDate] = useState<Date | undefined>(undefined);
    const [endDate, setEndDate] = useState<Date | undefined>(undefined);
    const [telegram, setTelegram] = useState<boolean>(false);
    const [headhunter, setHeadhunter] = useState<boolean>(false);
    const [superjob, setSuperjob] = useState<boolean>(false);


    const formatDateToUTCMoscow = (date?: Date): string | undefined => {
        if (!date) return undefined;
        return new Date(date.getTime() + 3 * 60 * 60 * 1000)
            .toISOString()
            .replace('Z', '+03:00');
    };


    const handle = async () => {
        const experienceNum = Number(experience);
        const minSalaryNum = Number(minSalary);
        const maxSalaryNum = Number(maxSalary);

        if (
            isNaN(experienceNum) || experienceNum < 0 ||
            isNaN(minSalaryNum) || minSalaryNum < 0 ||
            isNaN(maxSalaryNum) || maxSalaryNum < 0
        ) {
            toast.error("Пожалуйста, введите корректные числовые значения");
            return;
        }

        setFilters(prev => ({
            ...prev,
            vacancy: vacancy,
            experience: experience === "" ? 0 : experienceNum,
            city: city,
            minSalary: minSalary === "" ? 0 : minSalaryNum,
            maxSalary: maxSalary === "" ? 0 : maxSalaryNum,
            startDate: formatDateToUTCMoscow(startDate),
            endDate: formatDateToUTCMoscow(endDate),
            telegram: telegram,
            headhunter: headhunter
        }))
    }

    const clear = async () => {
        setVacancy("");
        setExperience("")
        setCity("")
        setStartDate(undefined)
        setEndDate(undefined)
        setMinSalary("")
        setMaxSalary("")
        setTelegram(false)
        setHeadhunter(false)
        setSuperjob(false)
    }

    return (
        <SidebarGroup>
            <SidebarGroupContent className="flex flex-col gap-2">
                <SidebarMenu>
                    <SidebarMenuItem className="flex items-center gap-2">
                        <SidebarMenuButton
                            tooltip="Filters"
                            className="justify-center bg-primary text-primary-foreground hover:bg-primary/90 hover:text-primary-foreground active:bg-primary/90 active:text-primary-foreground min-w-8 duration-200 ease-linear"
                            onClick={handle}
                        >
                            Поиск
                        </SidebarMenuButton>
                        <Button
                            size="icon"
                            className="size-8 group-data-[collapsible=icon]:opacity-0"
                            variant="outline"
                            onClick={clear}
                        >
                            <IconRefresh />
                        </Button>
                    </SidebarMenuItem>
                </SidebarMenu>
                <SidebarMenu>
                    <Label htmlFor="basic" className="px-1 pt-3 pb-1">
                        Основные параметры
                    </Label>
                    <Input type="text" value={vacancy} placeholder="Вакансия" onChange={(e) => setVacancy(e.target.value)}/>
                    <Input type="text" value={experience} placeholder="Стаж" className="appearance-none" onChange={(e) => setExperience(e.target.value)}/>
                    <SelectDemo city={city} setCity={setCity}/>

                    <Label htmlFor="basic" className="px-1 pt-3 pb-1">
                        Зарплата
                    </Label>
                    <Input type="text" placeholder="Минимальная" value={minSalary} onChange={(e) => setMinSalary(e.target.value)}/>
                    <Input type="text" placeholder="Максимальная" value={maxSalary} onChange={(e) => setMaxSalary(e.target.value)}/>
                    <DatePicker description={"Дата начала"} setDate={setStartDate} date={startDate}/>
                    <DatePicker description={"Дата конца"} setDate={setEndDate} date={endDate}/>
                    <Label htmlFor="source" className="px-1 pt-3">
                        Выбор источников
                    </Label>
                    <div className="grid grid-cols-3 gap-3 w-full max-w-xs pt-2">
                        <Button
                            className={`h-full aspect-square ${!telegram ? "bg-transparent" : "" }`}
                            onClick={() => { setTelegram(!telegram) }}>
                            <img
                                src="/telegram.png"
                                alt="label"
                                className="h-auto w-full object-cover"
                            />
                        </Button>
                        <Button
                            className={`h-full aspect-square ${!headhunter ? "bg-transparent" : "" }`}
                            onClick={() => setHeadhunter(!headhunter)}>
                            <img
                                src="/hh.png"
                                alt="label"
                                className="h-auto w-full object-cover"
                            />
                        </Button>
                        <Button
                            className={`h-full aspect-square ${!superjob ? "bg-transparent" : "" }`}
                            onClick={() => setSuperjob(!superjob)}>
                            <img
                                src="/sj.png"
                                alt="label"
                                className="h-auto w-full object-cover"
                            />
                        </Button>
                    </div>
                </SidebarMenu>
            </SidebarGroupContent>
        </SidebarGroup>
    )
}
