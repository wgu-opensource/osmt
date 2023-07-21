import { Pipe, PipeTransform } from '@angular/core';
import { IJobCode } from "../metadata/job-code/Jobcode"

@Pipe({
  name: 'jobCodeParents'
})
export class JobCodeParentsPipe implements PipeTransform {

  transform(jobCode: IJobCode): string {
    return jobCode.code + " " + jobCode.targetNodeName;
  }

}
