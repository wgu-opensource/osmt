import { Pipe, PipeTransform } from '@angular/core';
import { IJobCode } from "../metadata/job-codes/Jobcode"

@Pipe({
  name: 'jobCodeParents'
})
export class JobCodeParentsPipe implements PipeTransform {

  transform(value: IJobCode[]): string {
    return value?.sort((a, b) => {
      if (a.code < b.code) {
        return -1
      } else if (a.code > b.code) {
        return 1
      }
      return 0
    }).map(jobCode => jobCode.code + " " + jobCode.targetNodeName).join(",");
  }

}
