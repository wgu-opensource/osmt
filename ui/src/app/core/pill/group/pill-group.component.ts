import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { SvgHelper, SvgIcon } from '../../SvgHelper';
import { AbstractPillControl } from '../pill-control';

/** Match `.m-pill-group-collapsible.collapsed` max-height in styles.scss */
const COLLAPSED_MAX_HEIGHT_PX = 100;

@Component({
  selector: 'app-pill-group',
  templateUrl: './pill-group.component.html',
})
export class PillGroupComponent<TValue extends AbstractPillControl>
  implements AfterViewInit, OnChanges, OnDestroy
{
  readonly caretIconHref = SvgHelper.path(SvgIcon.CHEVRON);

  @Input() pillControls: TValue[] = [];
  @Output() pillClicked: EventEmitter<TValue> = new EventEmitter();

  @ViewChild('pillList', { static: false })
  pillListRef?: ElementRef<HTMLUListElement>;

  /** True when pill list is taller than collapsed max-height (see CSS). */
  hasOverflow = false;

  collapsed = true;

  private resizeObserver?: ResizeObserver;
  private measureRaf = 0;

  constructor(
    private host: ElementRef<HTMLElement>,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngAfterViewInit(): void {
    this.scheduleMeasure();
    if (typeof ResizeObserver !== 'undefined') {
      this.ngZone.runOutsideAngular(() => {
        this.resizeObserver = new ResizeObserver(() => {
          this.ngZone.run(() => this.scheduleMeasure());
        });
        this.resizeObserver.observe(this.host.nativeElement);
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pillControls']) {
      this.scheduleMeasure();
    }
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
    if (this.measureRaf) {
      cancelAnimationFrame(this.measureRaf);
    }
  }

  get hasPillClickedObservers(): boolean {
    return this.pillClicked.observers.length > 0;
  }

  toggleCollapse(event: Event): void {
    this.collapsed = !this.collapsed;
    event.preventDefault();
    event.stopPropagation();
  }

  expand(): void {
    this.collapsed = false;
  }

  /**
   * Expand when collapsed and user clicks outside pill buttons (legacy affordance).
   */
  onWrapperClick(event: Event): void {
    if (!this.hasOverflow || !this.collapsed) {
      return;
    }
    if ((event.target as HTMLElement).closest('button')) {
      return;
    }
    this.expand();
  }

  onPillClicked(pill: TValue): void {
    this.pillClicked.emit(pill);
  }

  private scheduleMeasure(): void {
    if (this.measureRaf) {
      cancelAnimationFrame(this.measureRaf);
    }
    this.measureRaf = requestAnimationFrame(() => {
      this.measureRaf = 0;
      this.measureOverflow();
    });
  }

  private measureOverflow(): void {
    const ul = this.pillListRef?.nativeElement;
    if (!ul || this.pillControls.length < 1) {
      this.applyHasOverflow(false);
      return;
    }
    // scrollHeight reflects full content even when parent applies max-height
    this.applyHasOverflow(ul.scrollHeight > COLLAPSED_MAX_HEIGHT_PX);
  }

  private applyHasOverflow(value: boolean): void {
    if (this.hasOverflow === value) {
      return;
    }
    const wasOverflow = this.hasOverflow;
    this.hasOverflow = value;
    if (!this.hasOverflow) {
      this.collapsed = false;
    } else if (!wasOverflow) {
      this.collapsed = true;
    }
    this.cdr.detectChanges();
  }
}
