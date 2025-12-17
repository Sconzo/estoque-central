import { Component } from '@angular/core';
import { PrimaryButtonComponent } from '../shared/components/buttons/primary-button/primary-button.component';
import { MetricCardComponent } from '../shared/components/feedback/metric-card/metric-card.component';

@Component({
  selector: 'app-component-showcase',
  standalone: true,
  imports: [PrimaryButtonComponent, MetricCardComponent],
  templateUrl: './component-showcase.component.html',
  styleUrl: './component-showcase.component.scss'
})
export class ComponentShowcaseComponent {
  isLoading = false;

  handleClick() {
    console.log('Button clicked!');
    this.isLoading = true;
    setTimeout(() => {
      this.isLoading = false;
    }, 2000);
  }

  handleSaveClick() {
    console.log('Save button clicked!');
  }

  handleDeleteClick() {
    console.log('Delete button clicked!');
  }
}
