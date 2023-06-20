import { Pipe, PipeTransform } from '@angular/core';
import { IJobCode } from "../metadata/job-codes/Jobcode"

@Pipe({
  name: 'jobCodeParents'
})
export class JobCodeParentsPipe implements PipeTransform {

  transform(jobCode: IJobCode): string {
    return jobCode.code + " " + jobCode.targetNodeName;
  }

}
