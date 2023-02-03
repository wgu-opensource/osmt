import {Pipe, PipeTransform} from "@angular/core"
import {PublishStatus} from "../PublishStatus"

@Pipe({
  name: "collection"
})
export class CollectionPipe implements PipeTransform {
  transform(value: PublishStatus | undefined, includesMy?: boolean): string {
    console.log(value)
    const isWorkspace = value === PublishStatus.Workspace
    if (isWorkspace) {
      return includesMy ? "My Workspace" : "Workspace"
    }
    return "Collection"
  }

}
